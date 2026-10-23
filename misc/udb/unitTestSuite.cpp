/*
 * unitTestSuite.cpp
 *
 *  Created on: Oct 22, 2023
 *      Author: cozyrohan
 */


#include "unitTestSuite.hpp"
#include <cassert>
#include <math.h>
#include "phasedArray.hpp"



// Speed of sound, freq, num_transdoozers, wavelength, dist b/w
PhasedArray tc{343, 39000, 7, 0, 0};

PhasedArray tc1{345, 39000, 7, 0, 0};


PhasedArray determine_res{343, 39000, 8, 0, 0};


bool doubles_equal_to_n_places(double d1, double d2, int num_dec)
{
	int i1 = d1 * pow(10, num_dec);
	int i2 = d2 * pow(10, num_dec);

	return i1 == i2;
}


bool UnitTestSuite::test_0_test_deg_rad_conversion()
{

	//manual verif
	doubles_equal_to_n_places(231.1, 231.1, 1);


	return (tc.deg_to_rad(0)==0);
}

bool UnitTestSuite::test_1_test_deg_rad_conversion()
{

	double v = tc.deg_to_rad(114.592);

	return (doubles_equal_to_n_places(v, 2.000, 3));
}

bool UnitTestSuite::test_2_test_deg_rad_conversion()
{

	double v = tc.deg_to_rad(359.5);

	return (doubles_equal_to_n_places(v, 6.274, 3));
}





bool UnitTestSuite::test_3_test_rad_deg_conversion()
{
	return (tc.rad_to_deg(0)==0);
}
bool UnitTestSuite::test_4_test_rad_deg_conversion()
{
	double v = tc.rad_to_deg(4.5);
	return doubles_equal_to_n_places(v, 257.831, 3);
}

bool UnitTestSuite::test_5_test_rad_deg_conversion()
{
	double v = tc.rad_to_deg(120.2);
	return  doubles_equal_to_n_places(v, 6886.9526, 4);
}




bool UnitTestSuite::test_6_test_wavelength_calc()
{

	tc.set_wavelength();
	return doubles_equal_to_n_places(tc.wavelength, tc.speed_of_sound/tc.frequency, 7)&&
			 doubles_equal_to_n_places(tc.wavelength,0.0087948 , 7);

}
bool UnitTestSuite::test_7_test_wavelength_calc()
{

	tc1.set_wavelength();
	return doubles_equal_to_n_places(tc1.wavelength, tc1.speed_of_sound/tc1.frequency, 7) &&
			 doubles_equal_to_n_places(tc1.wavelength,0.0088461 , 7);
}


bool UnitTestSuite::test_8_test_distance_calc()
{
	tc.set_wavelength();
	tc.set_transducer_distance();
	return doubles_equal_to_n_places(tc.d, tc.wavelength/2.0, 5);
}
bool UnitTestSuite::test_9_test_distance_calc()
{
	tc1.set_wavelength();
	tc1.set_transducer_distance();
	return doubles_equal_to_n_places(tc1.d, tc1.wavelength/2.0, 5);
}



bool UnitTestSuite::test_10_test_time_delay_calc()
{
	tc.set_wavelength();
	tc.set_transducer_distance();

	double td = tc.calc_time_delay_amount(30);


	return doubles_equal_to_n_places(td, 6.339, 3);;
}
bool UnitTestSuite::test_11_test_time_delay_calc()
{
	tc.set_wavelength();
	tc.set_transducer_distance();

	double td = tc.calc_time_delay_amount(15);


	return doubles_equal_to_n_places(td, 3.276, 3);
}



bool UnitTestSuite::test_12_test_time_delay_calc()
{
	determine_res.set_wavelength(); //  lambda = 8.79mm
	determine_res.set_transducer_distance(0.0098); //in meters -- 9.8mm x 10^03

	double td = tc.calc_time_delay_amount(10);
	 td = tc.calc_time_delay_amount(20);
	 td = tc.calc_time_delay_amount(30);
	 td = tc.calc_time_delay_amount(40);



}
bool UnitTestSuite::test_13_test_time_delay_calc()
{
	determine_res.set_wavelength(); //  lambda = 8.79mm
	determine_res.set_transducer_distance(0.0098); //in meters -- 9.8mm x 10^03

	double td = tc.calc_time_delay_amount(5);


}

bool* UnitTestSuite::test_all()
{
	for(int i = 0; i < num_tests; i++)
	{
		//must use this because member functions NEED instances to be called on (not static)
		if (   (this->*UnitTestSuite::tests[i])())
		{
			tests_status[i] = true;
		}
		else
		{
			tests_status[i] = false;
		}
	}


	return tests_status;
}





