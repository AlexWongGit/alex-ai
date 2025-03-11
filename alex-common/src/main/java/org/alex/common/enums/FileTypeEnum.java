package org.alex.common.enums;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/11
 */
@SuppressWarnings("all")
public enum FileTypeEnum {

    UNKNOWN("unknown"),

    PDF("pdf"),
    DOC("doc"),
    DOCX("docx"),
    PPT("ppt"),
    PPTX("pptx"),
    XLS("xls"),
    XLSX("xlsx"),
    TXT("txt"),
    CSV("csv"),
    MD("md"),
    HTML("html"),
    JSON("json"),
    YAML("yaml"),
    XML("xml"),
    SQL("sql"),
    PY("py"),
    JAVA("java"),
    CPP("cpp");

    private final String type;

    FileTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static FileTypeEnum getFileType(String fileName) {
        if (fileName == null) {
            return UNKNOWN;
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        for (FileTypeEnum value : values()) {
            if (value.getType().equals(fileType)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
