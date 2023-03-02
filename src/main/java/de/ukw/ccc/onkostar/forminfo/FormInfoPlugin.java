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

package de.ukw.ccc.onkostar.forminfo;

import de.itc.onkostar.api.Disease;
import de.itc.onkostar.api.Procedure;
import de.itc.onkostar.api.analysis.AnalyzerRequirement;
import de.itc.onkostar.api.analysis.IProcedureAnalyzer;
import de.itc.onkostar.api.analysis.OnkostarPluginType;
import de.ukw.ccc.onkostar.forminfo.services.FormInfoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Implementation of this plugin
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
@Component
public class FormInfoPlugin implements IProcedureAnalyzer {

    private final FormInfoService service;

    FormInfoPlugin(final FormInfoService service) {
        this.service = service;
    }

    @Override
    public OnkostarPluginType getType() {
        return OnkostarPluginType.BACKEND_SERVICE;
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public String getName() {
        return "CCC-MF FormInfo Plugin";
    }

    @Override
    public String getDescription() {
        return "Plugin to provide form and procedure informations";
    }

    @Override
    public boolean isSynchronous() {
        return false;
    }

    @Override
    public AnalyzerRequirement getRequirement() {
        return AnalyzerRequirement.PROCEDURE;
    }

    @Override
    public boolean isRelevantForDeletedProcedure() {
        return false;
    }

    @Override
    public boolean isRelevantForAnalyzer(Procedure procedure, Disease disease) {
        return false;
    }

    @Override
    public void analyze(Procedure procedure, Disease disease) {
        // Nothing to do - should never be called
    }

    /**
     * This method returns the content of given form
     *
     * @param data Data map containing procedure ID of the requested form.
     * @return List containing content data
     */
    public List<Result> getContent(Map<String, Object> data) {
        var id = data.get("id");
        if (null == id) {
            throw new RuntimeException("Missing value for 'id'");
        }

        try {
            return service.getFormInfo(Integer.parseInt(id.toString()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Invalid procedure id: %s", id));
        }
    }

    /**
     * This method returns the procedure ID of the related main form for given subform
     *
     * @param data Data map containing ID of a subform.
     * @return procedure ID of the related main form
     */
    public int getMainFormProcedureId(Map<String, Object> data) {
        var id = data.get("id");
        if (null == id) {
            throw new RuntimeException("Missing value for 'id'");
        }

        try {
            return service.getMainFormProcedureId(Integer.parseInt(id.toString()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Invalid procedure id: %s", id));
        }
    }
}
