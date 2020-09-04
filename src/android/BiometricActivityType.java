package de.niklasmerz.cordova.biometric;

public enum BiometricActivityType {
    JUST_AUTHENTICATE(1),
    REGISTER_SECRET(2),
    LOAD_SECRET(3);

    private int value;

    BiometricActivityType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BiometricActivityType fromValue(int val) {
        for (BiometricActivityType type : values()) {
            if (type.getValue() == val) {
                return type;
            }
        }
        return null;
    }
}
