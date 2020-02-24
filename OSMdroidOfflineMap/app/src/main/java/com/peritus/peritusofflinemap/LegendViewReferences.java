package com.peritus.peritusofflinemap;

import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class LegendViewReferences {

    private List<LinearLayout> mLegendItemLayouts;
    private List<CheckBox> mLegendCheckboxes;

    public LegendViewReferences() {
        this.mLegendItemLayouts = new ArrayList<>();
        this.mLegendCheckboxes = new ArrayList<>();
    }

    public List<LinearLayout> getLegendItemLayouts() {
        return mLegendItemLayouts;
    }

    public void setLegendItemLayouts(List<LinearLayout> legendItemLayouts) {
        this.mLegendItemLayouts.addAll(legendItemLayouts);
    }

    public List<CheckBox> getLegendCheckboxes() {
        return mLegendCheckboxes;
    }

    public void setLegendCheckboxes(List<CheckBox> legendCheckboxes) {
        this.mLegendCheckboxes.addAll(legendCheckboxes);
    }
}
