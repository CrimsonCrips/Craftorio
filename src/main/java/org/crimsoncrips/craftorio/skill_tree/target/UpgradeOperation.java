package org.crimsoncrips.craftorio.skill_tree.target;

public enum UpgradeOperation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE;

    public static double net(UpgradeOperation requested, double add, double subtract, double multiply, double divide) {
        if (requested == ADD || requested == SUBTRACT) {
            return add - subtract;
        }
        return (1.0 + multiply) / Math.max(1.0 + divide, 1.0E-6) - 1.0;
    }

    public static int slot(UpgradeOperation operation) {
        return operation.ordinal();
    }
}
