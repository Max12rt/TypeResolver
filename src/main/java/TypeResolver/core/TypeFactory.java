package TypeResolver.core;


import TypeResolver.UnionType;

import java.util.List;

public class TypeFactory {
    public static PhpType createType(String typeName) {
        return () -> typeName;
    }

    public static PhpType createUnionType(List<PhpType> types) {
        return new UnionType(types);
    }
}
