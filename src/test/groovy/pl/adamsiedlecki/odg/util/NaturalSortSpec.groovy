package pl.adamsiedlecki.odg.util

import spock.lang.Specification

class NaturalSortSpec extends Specification {

    def "should compare"() {

        when:
            def result = NaturalSort.compare(a, b)

        then:
            result == expectedResult

        where:
        a           | b             | expectedResult
        "abc"       | "abc"         | 0
        "abc"       | "bca"         | -1
        "stacja1"   | "stacja2"     | -1
        "stacja1"   | "stacja101"   | -1
    }
}
