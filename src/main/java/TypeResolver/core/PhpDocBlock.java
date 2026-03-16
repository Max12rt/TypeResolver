package TypeResolver.core;

import java.util.List;
public interface PhpDocBlock {
    List<DocTag> getTagsByName(String tagName);
}
