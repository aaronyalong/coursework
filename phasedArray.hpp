/*
 * testClass.hpp
 *
 *  Created on: Oct 21, 2023
 *      Author: cozyrohan
 */

#ifndef SRC_PHASEDARRAY_HPP_
#define SRC_PHASEDARRAY_HPP_


class PhasedArray
{
public:
	double speed_of_sound = 343; 						// 	shpeed of sound								(m/s)


	double frequency = 39000;						// frequency of the wave						(Hz)

	int number_transducers = 7;						// frequency of the wave						(Hz)


	double wavelength = 0;		//**set this again by calling set_wavelength    (m)

	double d = 0;  							// distance bw centers of adjacent transducers 	(m)




	//functions
	PhasedArray(double sos, int freq, int num_t, double wl, double dist_t);

	double get_frequency()
	{
		return frequency;
	}
	double get_number_transducers()
	{
		return number_transducers;
	}
	double get_wavelength()
	{
		return wavelength;
	}
	double get_d()
	{
		return d;
	}

	void set_wavelength();

	void set_transducer_distance();

	void set_transducer_distance(double dist_meters);

	double deg_to_rad(double deg);

	double rad_to_deg(double rad);

	double calc_time_delay_amount(double theta);
};


#endif /* SRC_TESTCLASS_HPP_ */
