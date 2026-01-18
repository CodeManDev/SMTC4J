package dev.codeman.smtc4j;

public record PlaybackState(int stateCode, double position) {
    public StateCode getStateCode() {
        return StateCode.fromCode(this.stateCode);
    }

    @Override
    public String toString() {
        return "PlaybackState{" +
                "stateCode=" + this.stateCode +
                ", position=" + this.position +
                '}';
    }

    public enum StateCode {
        UNKNOWN(-1),
        STOPPED(0),
        PAUSED(1),
        PLAYING(2),
        ;

        private final int code;

        StateCode(int code) {
            this.code = code;
        }

        public static StateCode fromCode(int code) {
            for (StateCode stateCode : values()) {
                if (stateCode.code == code) {
                    return stateCode;
                }
            }
            return UNKNOWN;
        }
    }
}