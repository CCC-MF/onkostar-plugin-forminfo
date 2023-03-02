package de.ukw.ccc.onkostar.forminfo.services;

import de.ukw.ccc.onkostar.forminfo.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultFormInfoServiceTest {

    @Test
    void testShouldFlattenSingleEntry() {
        var actual = DefaultFormInfoService.flattenEntry(
                new DefaultFormInfoService.Entry("description", "Beschreibung", Type.INPUT)
        );

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).name).isEqualTo("description");
    }

    @Test
    void testShouldFlattenGroupEntry() {
        var actual = DefaultFormInfoService.flattenEntry(
                new DefaultFormInfoService.Entry("group1", "Feldgruppe1", Type.GROUP, List.of(
                        new DefaultFormInfoService.Entry("startdate", "Datum des Beginns", Type.INPUT),
                        new DefaultFormInfoService.Entry("enddate", "Datum des Endes", Type.INPUT)
                ))
        );

        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).name).isEqualTo("group1");
        assertThat(actual.get(1).name).isEqualTo("startdate");
        assertThat(actual.get(2).name).isEqualTo("enddate");
    }

    @Test
    void testShouldFlattenSectionEntry() {
        var actual = DefaultFormInfoService.flattenEntry(
                new DefaultFormInfoService.Entry("section1", "Section1", Type.SECTION, List.of(
                        new DefaultFormInfoService.Entry("meds", "Medikamente", Type.INPUT),
                        new DefaultFormInfoService.Entry("other", "Weitere Angaben", Type.INPUT)
                ))
        );

        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).name).isEqualTo("section1");
        assertThat(actual.get(1).name).isEqualTo("meds");
        assertThat(actual.get(2).name).isEqualTo("other");
    }

}
