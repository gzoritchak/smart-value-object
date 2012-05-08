package org.bsf.smartValueObject;

import org.bsf.smartValueObject.container.SmartCollection;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Baseclass for TestVOs. Testcases could extend an add
 * needed behaviour. This VO is a mock for byte code enhanced
 * VOs to ease testing.
 */
public class TestVO implements Versionable {
    private Versionable v = new Version();

    // we need public access for testing purposes
    public int id;
    public String name;
    public TestVO otherTestVO;
    public Collection tests = new SmartCollection(new ArrayList(), new Version());

    public void setId(int id) {
        touch("id");
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setName(String s) {
        touch("name");
        this.name = s;
    }

    public String getName() {
        return this.name;
    }

    public void setOtherTestVO(TestVO test) {
        touch("otherTestVO");
        otherTestVO = test;
    }

    public boolean addTestVO(TestVO test) {
        return tests.add(test);
    }

    public boolean removeTestVO(TestVO test) {
        return tests.remove(test);
    }

    public void touch() {
        v.touch();
    }

    public void touch(String field) {
        v.touch(field);
    }

    public void delete() {
        v.delete();
    }

    public void create() {
        v.create();
    }

    public boolean isCreated() {
        return v.isCreated();
    }

    public boolean isDeleted() {
        return v.isDeleted();
    }

    public boolean isDirty() {
        return v.isDirty();
    }

    public void markClean() {
        v.markClean();
    }

    /**
     * Gets the version number.
     */
    public long getVersionId() {
        return v.getVersionId();
    }

    public void setVersionId(long id) {
        v.setVersionId(id);
    }

    public String toString() {
        return "TestVO: id=" + id;
    }
}