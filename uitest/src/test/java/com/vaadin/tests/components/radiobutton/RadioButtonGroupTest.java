/*
 * Copyright 2000-2014 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.components.radiobutton;

import com.vaadin.testbench.customelements.RadioButtonGroupElement;
import com.vaadin.tests.tb3.MultiBrowserTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test for RadioButtonGroup
 *
 * @author Vaadin Ltd
 * @since 8.0
 */
public class RadioButtonGroupTest extends MultiBrowserTest {

    @Before
    public void setUp() throws Exception {
        openTestURL();
    }

    @Test
    public void initialLoad_containsCorrectItems() {
        assertItems(20);
    }

    @Test
    public void initialItems_reduceItemCount_containsCorrectItems() {
        selectMenuPath("Component", "Data source", "Items", "5");
        assertItems(5);
    }

    @Test
    public void initialItems_increaseItemCount_containsCorrectItems() {
        selectMenuPath("Component", "Data source", "Items", "100");
        assertItems(100);
    }

    @Test
    public void clickToSelect() {
        selectMenuPath("Component", "Listeners", "Selection listener");

        getSelect().selectByText("Item 4");
        Assert.assertEquals("1. Selected: Optional[Item 4]", getLogRow(0));

        getSelect().selectByText("Item 2");
        Assert.assertEquals("2. Selected: Optional[Item 2]", getLogRow(0));

        getSelect().selectByText("Item 4");
        Assert.assertEquals("3. Selected: Optional[Item 4]", getLogRow(0));
    }

    @Test
    public void selectProgramatically() {
        selectMenuPath("Component", "Listeners", "Selection listener");

        selectMenuPath("Component", "Selection", "Toggle Item 5");
        Assert.assertEquals("2. Selected: Optional[Item 5]", getLogRow(0));
        assertSelected("Item 5");

        selectMenuPath("Component", "Selection", "Toggle Item 1");
        Assert.assertEquals("4. Selected: Optional[Item 1]", getLogRow(0));
        // DOM order
        assertSelected("Item 1");

        selectMenuPath("Component", "Selection", "Toggle Item 5");
        Assert.assertEquals("6. Selected: Optional[Item 5]", getLogRow(0));
        assertSelected("Item 5");
    }

    private void assertSelected(String... expectedSelection) {
        Assert.assertEquals(Arrays.asList(expectedSelection),
                getSelect().getSelection());
    }

    @Override
    protected Class<?> getUIClass() {
        return RadioButtonGroupTestUI.class;
    }

    protected RadioButtonGroupElement getSelect() {
        return $(RadioButtonGroupElement.class).first();
    }

    protected void assertItems(int count) {
        int i = 0;
        for (String text : getSelect().getOptions()) {
            assertEquals("Item " + i, text);
            i++;
        }
        assertEquals("Number of items", count, i);
    }
}
