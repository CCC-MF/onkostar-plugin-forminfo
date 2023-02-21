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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
     * This method returns content of given form
     *
     * @param data Data map containing procedure ID of requested form.
     * @return List containing content data
     */
    public List<Result> getContent(Map<String, Object> data) {
        var id = 0;
        try {
            id = Integer.parseInt(data.get("id").toString());
            return service.getFormInfo(id);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Invalid procedure id: {}", data.get("id")));
        }
    }
}
