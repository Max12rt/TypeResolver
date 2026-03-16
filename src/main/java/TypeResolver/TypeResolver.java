package TypeResolver;

import TypeResolver.core.DocTag;
import TypeResolver.core.PhpType;
import TypeResolver.core.PhpVariable;
import TypeResolver.core.TypeFactory;

import java.util.Arrays;
import java.util.List;

public class TypeResolver {

    public PhpType inferTypeFromDoc(PhpVariable variable) {
        var docBlock = variable.getDocBlock();
        if (docBlock == null)
            return TypeFactory.createType("mixed");

        String targetName = variable.getName();
        List<DocTag> varTags = docBlock.getTagsByName("var");

        for (var tag : varTags) {
            String value = tag.getValue();
            if (value == null || value.isBlank())
                continue;

            String[] parts = value.trim().split("\\s+");
            String typeContent = parts[0];

            if (parts.length > 1) {
                String tagName = parts[1];
                if (tagName.startsWith("$") && !tagName.equals(targetName)) {
                    continue;
                }
            }

            return parseType(typeContent);
        }

        return TypeFactory.createType("mixed");
    }

    private PhpType parseType(String typeContent) {
        if (typeContent.contains("|")) {
            var types = Arrays.stream(typeContent.split("\\|"))
                    .filter(s -> !s.isBlank())
                    .map(TypeFactory::createType)
                    .toList();

            if (types.isEmpty())
                return TypeFactory.createType("mixed");


            if (types.size() == 1)
                return types.get(0);
            else
                return TypeFactory.createUnionType(types);
        }
        return TypeFactory.createType(typeContent);
    }
}
