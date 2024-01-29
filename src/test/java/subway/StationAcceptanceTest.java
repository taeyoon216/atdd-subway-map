package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StationAcceptanceTest {

    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        Map<String, String> params = new HashMap<>();
        params.put("name", "강남역");

        ExtractableResponse<Response> response =
            RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/stations")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> stationNames =
            RestAssured.given().log().all()
                .when().get("/stations")
                .then().log().all()
                .extract().jsonPath().getList("name", String.class);
        assertThat(stationNames).containsAnyOf("강남역");
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @DisplayName("지하철역 목록을 조회한다.")
    @Test
    void getStationList() {
        // given
        saveStation("강남역");
        saveStation("교대역");

        // when
        ExtractableResponse<Response> response = getStations();

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.body().as(List.class)).hasSize(3);
            assertThat(response.jsonPath().getList("name")).containsAnyOf("강남역", "교대역");
        });

    }


    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역을 삭제한다.")
    @Test
    void deleteStation() {
        // given
        ExtractableResponse<Response> saveResponse = saveStation("강남역");
        Integer deleteStationId = saveResponse.jsonPath().get("id");

        // when
        ExtractableResponse<Response> response =
            RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/stations/{stationId}", deleteStationId)
                .then().log().all()
                .extract();

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(getStations().body().as(List.class)).isEmpty();
        });

    }


    private ExtractableResponse<Response> getStations() {
        return RestAssured.given().log().all()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/stations")
            .then().log().all()
            .extract();
    }

    private ExtractableResponse<Response> saveStation(String name) {
        return RestAssured.given()
            .body(Map.of("name", name))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/stations")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();
    }

}
