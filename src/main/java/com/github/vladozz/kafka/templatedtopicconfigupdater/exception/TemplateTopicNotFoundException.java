package com.github.vladozz.kafka.templatedtopicconfigupdater.exception;

public class TemplateTopicNotFoundException extends RuntimeException {
    private String name;

    public TemplateTopicNotFoundException(String name) {
        this.name = name;
    }

    public TemplateTopicNotFoundException(Throwable cause, String name) {
        super(cause);
        this.name = name;
    }

    @Override
    public String getMessage() {
        return String.format("Template topic with name %s is not found", name);
    }

}
