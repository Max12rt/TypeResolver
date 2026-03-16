package TypeResolver;


import TypeResolver.core.PhpDocBlock;
import TypeResolver.core.PhpVariable;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TypeResolver resolver = new TypeResolver();


        PhpVariable userVar = createMockVar("$user", "User");
        System.out.println("Test 1 ($user): " + resolver.inferTypeFromDoc(userVar).getTypeName());


        PhpVariable idVar = createMockVar("$id", "string|int");
        System.out.println("Test 2 ($id): " + resolver.inferTypeFromDoc(idVar).getTypeName());


        PhpVariable logVar = createMockVar("$log", "Logger $log");
        System.out.println("Test 3 ($log): " + resolver.inferTypeFromDoc(logVar).getTypeName());

        PhpVariable guestVar = createMockVar("$guest", "Admin $adm");
        System.out.println("Test 4 ($guest): " + resolver.inferTypeFromDoc(guestVar).getTypeName());
    }


    private static PhpVariable createMockVar(String name, String tagValue) {
        return new PhpVariable() {
            public String getName() { return name; }
            public PhpDocBlock getDocBlock() {
                return tagName -> List.of(() -> tagValue);
            }
        };
    }
}
