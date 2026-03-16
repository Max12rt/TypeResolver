package TypeResolver;


import TypeResolver.core.PhpType;

import java.util.List;
import java.util.stream.Collectors;

public record UnionType(List<PhpType> types) implements PhpType {
    @Override
    public String getTypeName() {
        return types.stream()
                .map(PhpType::getTypeName)
                .collect(Collectors.joining("|"));
    }
}

