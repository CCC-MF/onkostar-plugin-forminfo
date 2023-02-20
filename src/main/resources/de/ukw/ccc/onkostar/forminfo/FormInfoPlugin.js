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
                response.result.forEach((formEntry) => {
                    if (formEntry.type === 'BUTTON' || formEntry.type === 'FORM_REFERENCE' || formEntry.type === 'SUBFORM') {
                        return;
                    }
                    if (formEntry.type === 'SECTION') {
                        items.push({
                            html: `<div style="margin: 2px 2px 2px 0; border: 1px solid lightgray; background: linear-gradient(whitesmoke, lightgray); white-space: nowrap; font-weight: bold; font-size: 16px !important">${formEntry.description}</div>`,
                        });
                        items.push({
                            html: '<hr style="border-style: dashed"/>'
                        });
                    }
                    if (formEntry.type === 'GROUP') {
                        items.push({
                            html: `<div style="margin: 2px 2px 2px 0; white-space: nowrap; font-weight: bold; font-size: 16px !important">${formEntry.description}</div>`,
                        });
                        items.push({
                            html: '<hr style="border-style: dashed"/>'
                        });
                    }
                    if (formEntry.type === 'INPUT' && typeof formEntry.value == 'string' && formEntry.value.trim().length > 0) {
                        items.push({
                            cls: 'infoBoxLabel',
                            text: formEntry.description,
                        });
                        items.push({
                            text: formEntry.value,
                        });
                    }
                    if (formEntry.type === 'INPUT' && formEntry.value && typeof formEntry.value == 'object') {
                        items.push({
                            cls: 'infoBoxLabel',
                            text: formEntry.description,
                        });
                        items.push({
                            html: `<code style="color: gray">${JSON.stringify(formEntry.value)}</code>`,
                        });
                    }
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
                    columns: 2
                },
                bodyPadding: 8,
                defaults: {
                    xtype: 'label'
                },
                items: items
            });

            Ext.create('Ext.window.Window', {
                title: 'Info',
                height: 600,
                width: 800,
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