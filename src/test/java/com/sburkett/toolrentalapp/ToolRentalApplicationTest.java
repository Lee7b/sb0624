package com.sburkett.toolrentalapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sburkett.toolrentalapp.db.entity.ToolPricesDao;
import com.sburkett.toolrentalapp.db.entity.ToolsDao;
import com.sburkett.toolrentalapp.db.repository.ToolPricesRepository;
import com.sburkett.toolrentalapp.db.repository.ToolsRepository;
import com.sburkett.toolrentalapp.dto.CheckoutRequest;
import com.sburkett.toolrentalapp.services.CheckoutService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ToolRentalApplicationTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private ToolsRepository toolsRepository;

	@Autowired
	private ToolPricesRepository toolPricesRepository;

    @ParameterizedTest
	@CsvSource({
			"CHNS, Chainsaw, Stihl, 1.49, true, false, true",
			"LADW, Ladder, Werner, 1.99, true, true, false",
			"JAKD, Jackhammer, DeWalt, 2.99, true, false, false",
			"JAKR, Jackhammer, Ridgid, 2.99, true, false, false"
	})
    public void verifyToolRepositoryData(String toolCode, String expectedToolType, String expectedBrand, BigDecimal expectedDailyCharge,
								   boolean expectedWeekdayCharge, boolean expectedWeekendCharge, boolean expectedHolidayCharge) {
		ToolsDao toolsDao = toolsRepository.findByToolCode(toolCode);
		ToolPricesDao  toolPricesDao  = toolPricesRepository.findByToolType(toolsDao.getToolType());

		Assertions.assertEquals(toolPricesDao.getToolType(), expectedToolType);
		Assertions.assertEquals(toolsDao.getBrand(), expectedBrand);
		Assertions.assertEquals(toolPricesDao.getDailyCharge(), expectedDailyCharge);
		Assertions.assertEquals(toolPricesDao.isWeekdayCharge(), expectedWeekdayCharge);
		Assertions.assertEquals(toolPricesDao.isWeekendCharge(), expectedWeekendCharge);
		Assertions.assertEquals(toolPricesDao.isHolidayCharge(), expectedHolidayCharge);
	}

	@Test
	public void test1_shouldThrowErrorWhenDiscountIsOutOfRange() throws Exception {
		String expected = "{\"errorMessage\":\"discountPercent: discountPercent must be in range of 0-100\"}";
		mockMvc.perform(MockMvcRequestBuilders
						.post("/checkout")
						.content(objectMapper.writeValueAsString(CheckoutRequest.builder().toolCode("JAKR").checkoutDate("09/03/15").rentalDayCount(5).discountPercent(101).build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError())
				.andExpect(content().json(expected));
	}

	@Test
	public void test2_shouldReturnSuccess() throws Exception {
		String expected = "{\"toolCode\":\"LADW\",\"toolType\":\"Ladder\",\"toolBrand\":\"Werner\",\"rentalDays\":\"3\",\"checkOutDate\":\"07/02/20\",\"dueDate\":\"07/05/20\",\"dailyRentalCharge\":\"$1.99\",\"chargeDays\":\"2\",\"preDiscountCharge\":\"$3.98\",\"discountPercent\":\"10%\",\"discountAmount\":\"$0.40\",\"finalCharge\":\"$3.58\"}";
		mockMvc.perform(MockMvcRequestBuilders
						.post("/checkout")
						.content(objectMapper.writeValueAsString(CheckoutRequest.builder().toolCode("LADW").checkoutDate("07/02/20").rentalDayCount(3).discountPercent(10).build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(expected));
	}

	@Test
	public void test3_shouldReturnSuccess() throws Exception {
		String expected = "{\"toolCode\":\"CHNS\",\"toolType\":\"Chainsaw\",\"toolBrand\":\"Stihl\",\"rentalDays\":\"5\",\"checkOutDate\":\"07/02/15\",\"dueDate\":\"07/07/15\",\"dailyRentalCharge\":\"$1.49\",\"chargeDays\":\"3\",\"preDiscountCharge\":\"$4.47\",\"discountPercent\":\"25%\",\"discountAmount\":\"$1.12\",\"finalCharge\":\"$3.35\"}";
		mockMvc.perform(MockMvcRequestBuilders
						.post("/checkout")
						.content(objectMapper.writeValueAsString(CheckoutRequest.builder().toolCode("CHNS").checkoutDate("07/02/15").rentalDayCount(5).discountPercent(25).build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(expected));
	}

	@Test
	public void test4_shouldReturnSuccess() throws Exception {
		String expected = "{\"toolCode\":\"JAKD\",\"toolType\":\"Jackhammer\",\"toolBrand\":\"DeWalt\",\"rentalDays\":\"6\",\"checkOutDate\":\"09/03/15\",\"dueDate\":\"09/09/15\",\"dailyRentalCharge\":\"$2.99\",\"chargeDays\":\"3\",\"preDiscountCharge\":\"$8.97\",\"discountPercent\":\"0%\",\"discountAmount\":\"$0.00\",\"finalCharge\":\"$8.97\"}";
		mockMvc.perform(MockMvcRequestBuilders
						.post("/checkout")
						.content(objectMapper.writeValueAsString(CheckoutRequest.builder().toolCode("JAKD").checkoutDate("09/03/15").rentalDayCount(6).discountPercent(0).build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(expected));
	}

	@Test
	public void test5_shouldReturnSuccess() throws Exception {
		String expected = "{\"toolCode\":\"JAKR\",\"toolType\":\"Jackhammer\",\"toolBrand\":\"Ridgid\",\"rentalDays\":\"9\",\"checkOutDate\":\"07/02/15\",\"dueDate\":\"07/11/15\",\"dailyRentalCharge\":\"$2.99\",\"chargeDays\":\"5\",\"preDiscountCharge\":\"$14.95\",\"discountPercent\":\"0%\",\"discountAmount\":\"$0.00\",\"finalCharge\":\"$14.95\"}";
		mockMvc.perform(MockMvcRequestBuilders
						.post("/checkout")
						.content(objectMapper.writeValueAsString(CheckoutRequest.builder().toolCode("JAKR").checkoutDate("07/02/15").rentalDayCount(9).discountPercent(0).build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(expected));
	}

	@Test
	public void test6_shouldReturnSuccess() throws Exception {
		String expected = "{\"toolCode\":\"JAKR\",\"toolType\":\"Jackhammer\",\"toolBrand\":\"Ridgid\",\"rentalDays\":\"4\",\"checkOutDate\":\"07/02/20\",\"dueDate\":\"07/06/20\",\"dailyRentalCharge\":\"$2.99\",\"chargeDays\":\"1\",\"preDiscountCharge\":\"$2.99\",\"discountPercent\":\"50%\",\"discountAmount\":\"$1.50\",\"finalCharge\":\"$1.49\"}";
		mockMvc.perform(MockMvcRequestBuilders
						.post("/checkout")
						.content(objectMapper.writeValueAsString(CheckoutRequest.builder().toolCode("JAKR").checkoutDate("07/02/20").rentalDayCount(4).discountPercent(50).build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(expected));
	}

	@Test
	public void test7_shouldThrowErrorWhenRentalDaysIsZero() throws Exception {
		String expected = "{\"errorMessage\":\"rentalDayCount: rentalDayCount must be 1 or greater\"}";
		mockMvc.perform(MockMvcRequestBuilders
						.post("/checkout")
						.content(objectMapper.writeValueAsString(CheckoutRequest.builder().toolCode("JAKR").checkoutDate("09/03/15").rentalDayCount(0).discountPercent(0).build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError())
				.andExpect(content().json(expected));
	}
}
