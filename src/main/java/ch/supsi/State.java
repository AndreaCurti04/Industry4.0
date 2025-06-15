package ch.supsi;

public enum State {
    FULL {
        public String toString() {
            return "full";
        }
    },
    EMPTY {
        public String toString() {
            return "empty";
        }
    }
}
