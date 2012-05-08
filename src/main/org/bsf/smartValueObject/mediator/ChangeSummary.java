package org.bsf.smartValueObject.mediator;

/**
 * Encapsulates changes made by updateGraph.
 * 
 */
public class ChangeSummary {
    private Object key;
    private String report;

    public ChangeSummary(Object key, String report) {
        this.key = key;
        this.report = report;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
