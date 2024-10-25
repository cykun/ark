package ink.xikun.ark.protocol;

import lombok.Getter;

@Getter
public enum MsgStatus {

    SUCCESS(0),
    FAIL(1);

    private final int code;

    MsgStatus(int code) {
        this.code = code;
    }
}
