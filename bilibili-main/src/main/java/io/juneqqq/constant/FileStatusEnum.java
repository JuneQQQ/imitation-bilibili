package io.juneqqq.constant;

public enum FileStatusEnum {
    FILE_COMPLETELY_EXISTS("文件已存在"),
    FILE_PARTLY_EXISTS("部分存在或不存在"),
    FILE_NEED_MERGE("分片完全存在，需要合并"),

    FILE_NOT_EXISTS("文件完全不存在");

    private final String message;

    FileStatusEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
