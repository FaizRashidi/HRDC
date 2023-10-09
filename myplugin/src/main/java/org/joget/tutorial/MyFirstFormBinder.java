/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joget.tutorial;

import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;

/**
 *
 * @author Fawad Khaliq <khaliq@opendynamics.com.my>
 */
public class MyFirstFormBinder extends WorkflowFormBinder {

    public MyFirstFormBinder getSuper() {
        return this;
    }

    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {

        super.store(element, rows, formData);
        
        System.out.println("Hello World");

        return rows;
    }

    @Override
    public String getName() {
        return ("MyFirst Form Binder");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("MyFirst Form Binder");
    }

    @Override
    public String getLabel() {
        return ("MyFirst Form Binder");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormId() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_ID);
    }

    @Override
    public String getTableName() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
    }
}