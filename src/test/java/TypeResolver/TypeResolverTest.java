package TypeResolver;

import TypeResolver.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TypeResolverTest {

    private TypeResolver resolver;
    private PhpVariable variable;
    private PhpDocBlock docBlock;

    @BeforeEach
    void setUp() {
        resolver = new TypeResolver();
        variable = mock(PhpVariable.class);
        docBlock = mock(PhpDocBlock.class);
    }

    @Test
    @DisplayName("Standard type: /** @var User */ for $user")
    void testStandardType() {
        try (MockedStatic<TypeFactory> factoryMock = mockStatic(TypeFactory.class)) {
            setupVariable("$user", "User", factoryMock);

            PhpType result = resolver.inferTypeFromDoc(variable);

            assertEquals("User", result.getTypeName());
        }
    }

    @Test
    @DisplayName("Union type: /** @var string|int */")
    void testUnionType() {
        try (MockedStatic<TypeFactory> factoryMock = mockStatic(TypeFactory.class)) {
            String typeContent = "string|int";
            when(variable.getName()).thenReturn("$id");
            when(variable.getDocBlock()).thenReturn(docBlock);

            DocTag tag = mock(DocTag.class);
            when(tag.getValue()).thenReturn(typeContent);
            when(docBlock.getTagsByName("var")).thenReturn(Collections.singletonList(tag));

            PhpType stringType = new MockPhpType("string");
            PhpType intType = new MockPhpType("int");
            factoryMock.when(() -> TypeFactory.createType("string")).thenReturn(stringType);
            factoryMock.when(() -> TypeFactory.createType("int")).thenReturn(intType);
            factoryMock.when(() -> TypeFactory.createUnionType(anyList()))
                    .thenReturn(new MockPhpType("string|int"));

            PhpType result = resolver.inferTypeFromDoc(variable);
            assertEquals("string|int", result.getTypeName());
        }
    }

    @Test
    @DisplayName("Name mismatch: /** @var Admin $adm */ for $guest -> should return mixed")
    void testNameMismatch() {
        try (MockedStatic<TypeFactory> factoryMock = mockStatic(TypeFactory.class)) {
            when(variable.getName()).thenReturn("$guest");
            when(variable.getDocBlock()).thenReturn(docBlock);

            DocTag tag = mock(DocTag.class);
            when(tag.getValue()).thenReturn("Admin $adm");
            when(docBlock.getTagsByName("var")).thenReturn(Collections.singletonList(tag));

            factoryMock.when(() -> TypeFactory.createType("mixed")).thenReturn(new MockPhpType("mixed"));

            PhpType result = resolver.inferTypeFromDoc(variable);
            assertEquals("mixed", result.getTypeName());
        }
    }

    @Test
    @DisplayName("Select the correct tag based on variable name")
    void testMultipleTags() {
        try (MockedStatic<TypeFactory> factoryMock = mockStatic(TypeFactory.class)) {
            when(variable.getName()).thenReturn("$name");
            when(variable.getDocBlock()).thenReturn(docBlock);

            DocTag tag1 = mock(DocTag.class);
            when(tag1.getValue()).thenReturn("int $id");
            DocTag tag2 = mock(DocTag.class);
            when(tag2.getValue()).thenReturn("string $name");

            when(docBlock.getTagsByName("var")).thenReturn(Arrays.asList(tag1, tag2));
            factoryMock.when(() -> TypeFactory.createType("string")).thenReturn(new MockPhpType("string"));

            PhpType result = resolver.inferTypeFromDoc(variable);
            assertEquals("string", result.getTypeName());
        }
    }

    private void setupVariable(String varName, String tagValue, MockedStatic<TypeFactory> factoryMock) {
        when(variable.getName()).thenReturn(varName);
        when(variable.getDocBlock()).thenReturn(docBlock);
        DocTag tag = mock(DocTag.class);
        when(tag.getValue()).thenReturn(tagValue);
        when(docBlock.getTagsByName("var")).thenReturn(Collections.singletonList(tag));
        factoryMock.when(() -> TypeFactory.createType(anyString())).thenAnswer(inv -> new MockPhpType(inv.getArgument(0)));
    }

    @Test
    @DisplayName("Missing DocBlock -> should return mixed")
    void testNoDocBlock() {
        try (MockedStatic<TypeFactory> factoryMock = mockStatic(TypeFactory.class)) {
            when(variable.getDocBlock()).thenReturn(null);
            factoryMock.when(() -> TypeFactory.createType("mixed")).thenReturn(new MockPhpType("mixed"));

            assertEquals("mixed", resolver.inferTypeFromDoc(variable).getTypeName());
        }
    }

    @Test
    @DisplayName("Handle extra spaces: '  string   $id  '")
    void testExtraSpacesHandling() {
        try (MockedStatic<TypeFactory> factoryMock = mockStatic(TypeFactory.class)) {
            setupVariable("$id", "   string    $id   ", factoryMock);
            factoryMock.when(() -> TypeFactory.createType("string")).thenReturn(new MockPhpType("string"));

            assertEquals("string", resolver.inferTypeFromDoc(variable).getTypeName());
        }
    }

    @Test
    @DisplayName("Generic tag (no name) is valid for any variable")
    void testGenericTag() {
        try (MockedStatic<TypeFactory> factoryMock = mockStatic(TypeFactory.class)) {
            setupVariable("$any", "Collection", factoryMock);
            factoryMock.when(() -> TypeFactory.createType("Collection")).thenReturn(new MockPhpType("Collection"));

            assertEquals("Collection", resolver.inferTypeFromDoc(variable).getTypeName());
        }
    }

    private static class MockPhpType implements PhpType {
        private final String name;
        MockPhpType(String name) { this.name = name; }
        @Override public String getTypeName() { return name; }
        @Override public String toString() { return name; }
    }
}
