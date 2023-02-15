/*
 * MIT License
 *
 * Copyright (c) 2023 Comprehensive Cancer Center Mainfranken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

class FormInfoPlugin {

    static showFormContentInfo(context, procedureId) {
        let items = [];

        context.executePluginMethod(
            'FormInfoPlugin',
            'getContent',
            { id: procedureId },
            (response) => {
                if (response.status.code < 0) {
                    onFailure();
                    return;
                }
                Object.entries(response.result).forEach((formElement) => {
                    if (formElement.length < 2) {
                        return;
                    }
                    if (formElement[1].description.trim().length === 0) {
                        items.push({
                            cls: 'infoBoxLabel',
                            text: '*' + formElement[1].field,
                        });
                    } else {
                        items.push({
                            cls: 'infoBoxLabel',
                            text: formElement[1].description,
                        });
                    }
                    items.push({
                        text: formElement[1].value,
                    });
                });
                showDialog();
            },
            false
        );

        const onFailure = () => {
            Ext.MessageBox.show({
                title: 'Hinweis',
                msg: 'Plugin "FormInfo" nicht verfügbar oder Fehler beim Ermitteln der Formularinhalte.',
                buttons: Ext.MessageBox.OKCANCEL
            });
        };

        const showDialog = () => {
            var table = Ext.create('Ext.panel.Panel', {
                layout: {
                    type: 'table',
                    // The total column count must be specified here
                    columns: 2
                },
                bodyPadding: 8,
                defaults: {
                    // applied to each contained panel
                    xtype: 'label'
                },
                items: items
            });

            Ext.create('Ext.window.Window', {
                title: 'Info',
                height: 400,
                width: 600,
                layout: 'fit',
                items: [table]
            }).show();
        }
    }
}



/**
 * Wrapper for use with ExtJS
 *
 * Usage example:
 *
 * let FormInfoPlugin = Ext.ClassManager.get('plugins.FormInfoPlugin');
 * FormInfoPlugin.showFormContentInfo(1234));
 */
Ext.define('FormInfoPlugin', {
    statics: {
        showFormContentInfo: FormInfoPlugin.showFormContentInfo
    }
});