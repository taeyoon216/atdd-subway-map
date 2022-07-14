package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LineAcceptanceTest extends BaseAcceptanceTest{

	@BeforeEach
	public void setUp() {
		지하철_역_생성("지하철역");
		지하철_역_생성("새로운지하철역");
		지하철_역_생성("또다른지하철역");
	}

	/**
	 * When 지하철 노선을 생성하면
	 * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다.
	 */
	@DisplayName("지하철노선 생성")
	@Test
	void createLine() {
		// when
		지하철_노선_생성("신분당선", "bg-red-600", 1L, 2L);
		// then
		List<String> lineNames =
				지하철_노선_목록_조회().jsonPath().getList("name");

		assertThat(lineNames).contains("신분당선");
	}

	/**
	 * Given 2개의 지하철 노선을 생성하고
	 * When 지하철 노선 목록을 조회하면
	 * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
	 */
	@DisplayName("지하철노선 목록 조회")
	@Test
	void showLines() {
		// given
		지하철_노선_생성("신분당선", "bg-red-600", 1L, 2L);
		지하철_노선_생성("분당선", "bg-green-600", 1L, 3L);

		// when
		List<String> lineNames = 지하철_노선_목록_조회().jsonPath().getList("name");

		// then
		assertThat(lineNames).contains("신분당선", "분당선");
	}

	/**
	 * Given 지하철 노선을 생성하고
	 * When 생성한 지하철 노선을 조회하면
	 * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
	 */
	@DisplayName("지하철노선 조회")
	@Test
	void showLine() {
		// given
		Integer lineId =
				지하철_노선_생성("신분당선", "bg-red-600", 1L, 2L).jsonPath().get("id");

		// when
		String lineName =
				지하철_노선_조회(lineId).jsonPath().get("name");

		// then
		assertThat(lineName).isEqualTo("신분당선");
	}

	/**
	 * Given 지하철 노선을 생성하고
	 * When 생성한 지하철 노선을 수정하면
	 * Then 해당 지하철 노선 정보는 수정된다.
	 */
	@DisplayName("지하철노선 수정")
	@Test
	void updateLine() {
		// given
		Integer LineId =
				지하철_노선_생성("신분당선", "bg-red-600", 1L, 2L).jsonPath().get("id");

		// when
		지하철_노선_수정(LineId, "다른분당선", "bg-red-600");

		// then
		String lineName =
				지하철_노선_조회(LineId).jsonPath().get("name");
		assertThat(lineName).isEqualTo("다른분당선");
	}

	/**
	 * Given 지하철 노선을 생성하고
	 * When 생성한 지하철 노선을 삭제하면
	 * Then 해당 지하철 노선 정보는 삭제된다.
	 */
	@DisplayName("지하철노선 삭제")
	@Test
	void deleteLine() {
		// given
		Integer lineId =
				지하철_노선_생성("신분당선", "bg-red-600", 1L, 2L).jsonPath().get("id");

		// when
		지하철_노선_삭제(lineId);

		// then
		List<Long> lineIds =
				지하철_노선_목록_조회().jsonPath().getList("id");
		assertThat(lineIds).isEmpty();
	}

	private ExtractableResponse<Response> 지하철_역_생성(String name) {
		Map<String, String> station = new HashMap<>();
		station.put("name", name);

		return RestAssured.given().log().all()
				.body(station)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.when().post("/stations")
				.then().log().all()
				.extract();
	}

	private ExtractableResponse<Response> 지하철_노선_생성(String name, String color, Long upStationId, Long downStationId) {
		Map<String, Object> params = new HashMap<>();
		params.put("name", name);
		params.put("color", color);
		params.put("upStationId", upStationId);
		params.put("downStationId", downStationId);

		return 지하철_노선_생성(params);
	}

	private ExtractableResponse<Response> 지하철_노선_생성(Map<String, Object> params) {
		 return RestAssured
				.given().log().all()
				.body(params)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.when().post("/lines")
				.then().log().all()
				.extract();
	}

	private ExtractableResponse<Response> 지하철_노선_조회(Integer id) {
		return RestAssured
				.given().log().all()
				.when().get("/lines/{lineId}", id)
				.then().log().all()
				.extract();
	}

	private ExtractableResponse<Response> 지하철_노선_목록_조회() {
		return RestAssured
				.given().log().all()
				.when().get("/lines")
				.then().log().all()
				.extract();
	}

	private ExtractableResponse<Response> 지하철_노선_수정(Integer LineId, String name, String color) {
		Map<String, String> line = new HashMap<>();
		line.put("name", name);
		line.put("color", color);

		return RestAssured
				.given().log().all()
				.body(line)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.when().put("/lines/{lineId}", LineId)
				.then().log().all()
				.extract();
	}

	private ExtractableResponse<Response> 지하철_노선_삭제(Integer id) {
		return RestAssured
				.given().log().all()
				.when().delete("/lines/{lineId}", id)
				.then().log().all()
				.extract();
	}
}
