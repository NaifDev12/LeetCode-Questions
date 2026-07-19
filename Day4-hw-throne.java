import java.util.*;

class ThroneInheritance {
    private final String king;
    private final Map<String, List<String>> familyTree;
    private final Set<String> deadSet;

    public ThroneInheritance(String kingName) {
        this.king = kingName;
        this.familyTree = new HashMap<>();
        this.deadSet = new HashSet<>();
    }
    
    public void birth(String parentName, String childName) {
        // If the parent doesn't have children yet, initialize the list
        familyTree.computeIfAbsent(parentName, k -> new ArrayList<>()).add(childName);
    }
    
    public void death(String name) {
        deadSet.add(name);
    }
    
    public List<String> getInheritanceOrder() {
        List<String> order = new ArrayList<>();
        dfs(king, order);
        return order;
    }

    private void dfs(String currentPerson, List<String> order) {
        // Add to order only if the person is alive
        if (!deadSet.contains(currentPerson)) {
            order.add(currentPerson);
        }
        
        // Recurse through children if they exist
        List<String> children = familyTree.get(currentPerson);
        if (children != null) {
            for (String child : children) {
                dfs(child, order);
            }
        }
    }
}
