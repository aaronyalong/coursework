/*
 * unitTestSuite.hpp
 *
 *  Created on: Oct 22, 2023
 *      Author: cozyrohan
 */

#ifndef SRC_UNITTESTSUITE_HPP_
#define SRC_UNITTESTSUITE_HPP_


class UnitTestSuite
{
public:
	typedef bool (UnitTestSuite::*TestFunc)();

	static const int num_tests = 14;
	TestFunc tests[num_tests] = {
								&UnitTestSuite::test_0_test_deg_rad_conversion,
								&UnitTestSuite::test_1_test_deg_rad_conversion,
								&UnitTestSuite::test_2_test_deg_rad_conversion,


								&UnitTestSuite::test_3_test_rad_deg_conversion,
								&UnitTestSuite::test_4_test_rad_deg_conversion,
								&UnitTestSuite::test_5_test_rad_deg_conversion,


								&UnitTestSuite::test_6_test_wavelength_calc,
								&UnitTestSuite::test_7_test_wavelength_calc,



								&UnitTestSuite::test_8_test_distance_calc,
								&UnitTestSuite::test_9_test_distance_calc,



								&UnitTestSuite::test_10_test_time_delay_calc,
								&UnitTestSuite::test_11_test_time_delay_calc,


								&UnitTestSuite::test_12_test_time_delay_calc,
								&UnitTestSuite::test_13_test_time_delay_calc,

	};

	bool tests_status[num_tests];

	// ALL TEST RETURN TRUE FOR PASS; FALSE OTHERWISE

	bool test_0_test_deg_rad_conversion();
	bool test_1_test_deg_rad_conversion();
	bool test_2_test_deg_rad_conversion();


	bool test_3_test_rad_deg_conversion();
	bool test_4_test_rad_deg_conversion();
	bool test_5_test_rad_deg_conversion();




	bool test_6_test_wavelength_calc();
	bool test_7_test_wavelength_calc();

	bool test_8_test_distance_calc();
	bool test_9_test_distance_calc();

	bool test_10_test_time_delay_calc();
	bool test_11_test_time_delay_calc();

	bool test_12_test_time_delay_calc();
	bool test_13_test_time_delay_calc();


	bool* test_all();

	const int get_num_tests()
	{return num_tests;}

};


#endif /* SRC_UNITTESTSUITE_HPP_ */
