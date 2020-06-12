/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cwcdh.pppp.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import cwcdh.pppp.enums.DataRepresentationType;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Transient;

/**
 *
 * @author buddhika
 */
@Entity
public class SiComponentItem extends SiComponent {

    
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Solution solution;
    @ManyToOne
    private Implementation implementation;
    @ManyToOne
    private SiFormSet itemFormset;
    @Enumerated(EnumType.STRING)
    private DataRepresentationType dataRepresentationType;
    @Transient
    String valueAsString;
    @Transient
    private String valueAsStringDisplay;
    @Transient
    private boolean displayAsLabel;
    @Transient
    private boolean displayAsLink;

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public Implementation getImplementation() {
        return implementation;
    }

    public void setImplementation(Implementation implementation) {
        this.implementation = implementation;
    }

    public SiFormSet getItemFormset() {
        return itemFormset;
    }

    public void setItemFormset(SiFormSet itemFormset) {
        this.itemFormset = itemFormset;
    }

    public DataRepresentationType getDataRepresentationType() {
        return dataRepresentationType;
    }

    public void setDataRepresentationType(DataRepresentationType dataRepresentationType) {
        this.dataRepresentationType = dataRepresentationType;
    }

    public String getValueAsString() {
        if (this.getItem() == null) {
            return "";
        }
        switch (this.getItem().getDataType()) {
            case Short_Text:
                return this.getShortTextValue();
            case Long_Text:
                return this.getLongTextValue();
            case Integer_Number:
                return this.getIntegerNumberValue().toString();
            case Long_Number:
                return this.getLongNumberValue().toString();
            case Real_Number:
                return this.getRealNumberValue().toString();
            case Item_Reference:
                if (this.getItemValue() != null) {
                    return this.getItemValue().getName();
                } else {
                    return "";
                }
            case Institution_Reference:
                if(this.getInstitutionValue()!=null){
                    return this.getInstitutionValue().getName();
                }else{
                    return "";
                }

        }
        return "";
    }

    public String getValueAsStringDisplay() {
        return valueAsStringDisplay;
    }

    public void setValueAsStringDisplay(String valueAsStringDisplay) {
        this.valueAsStringDisplay = valueAsStringDisplay;
    }

    
    private void findDisplayMethod(){
        displayAsLabel=false;
        displayAsLink=false;
        switch (this.getItem().getRenderType()) {
            case Link:
                displayAsLink=true;
                break;
            default:
                displayAsLabel=true;

        }
    }
    
    public boolean getDisplayAsLabel() {
        findDisplayMethod();
        return displayAsLabel;
    }

    
    public boolean getDisplayAsLink() {
        findDisplayMethod();
        return displayAsLink;
    }

   

    
    
}
