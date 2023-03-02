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

package de.ukw.ccc.onkostar.forminfo.services;

import de.ukw.ccc.onkostar.forminfo.FormInfoException;
import de.ukw.ccc.onkostar.forminfo.Result;

import java.util.List;

/**
 * Interface of all FormInfoService
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
public interface FormInfoService {
    /**
     * This method returns the content of given form
     *
     * @param procedureId The procedure ID of the requested form.
     * @return List containing content data
     */
    List<Result> getFormInfo(int procedureId) throws FormInfoException;

    /**
     * This method returns the procedure ID of the related main form for given subform
     *
     * @param procedureId The ID of a subform.
     * @return procedure ID of the related main form
     */
    int getMainFormProcedureId(int procedureId) throws FormInfoException;
}
