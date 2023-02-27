package de.ukw.ccc.onkostar.forminfo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FormInfoServiceTest {

    @Test
    void testShouldFlattenSingleEntry() {
        var actual = FormInfoService.flattenEntry(
                new FormInfoService.Entry("description", "Beschreibung", Type.INPUT)
        );

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).name).isEqualTo("description");
    }

    @Test
    void testShouldFlattenGroupEntry() {
        var actual = FormInfoService.flattenEntry(
                new FormInfoService.Entry("group1", "Feldgruppe1", Type.GROUP, List.of(
                        new FormInfoService.Entry("startdate", "Datum des Beginns", Type.INPUT),
                        new FormInfoService.Entry("enddate", "Datum des Endes", Type.INPUT)
                ))
        );

        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).name).isEqualTo("group1");
        assertThat(actual.get(1).name).isEqualTo("startdate");
        assertThat(actual.get(2).name).isEqualTo("enddate");
    }

    @Test
    void testShouldFlattenSectionEntry() {
        var actual = FormInfoService.flattenEntry(
                new FormInfoService.Entry("section1", "Section1", Type.SECTION, List.of(
                        new FormInfoService.Entry("meds", "Medikamente", Type.INPUT),
                        new FormInfoService.Entry("other", "Weitere Angaben", Type.INPUT)
                ))
        );

        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).name).isEqualTo("section1");
        assertThat(actual.get(1).name).isEqualTo("meds");
        assertThat(actual.get(2).name).isEqualTo("other");
    }

}
