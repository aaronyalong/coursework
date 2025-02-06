#include "TM4C123GH6PM.h"

void Delay_ms(int n);

int main(void) {
    int duty_cycle = 4999;
    int adc_value;

    // Clock setting for PWM and GPIO PORT
    SYSCTL->RCGCPWM |= 2;        // Enable clock to PWM1 module
    SYSCTL->RCGCGPIO |= 0x20;    // Enable system clock to PORTF
    SYSCTL->RCC |= (1 << 20);    // Enable System Clock Divisor function
    SYSCTL->RCC |= 0x000E0000;   // Use pre-divider value of 64 and after that feed clock to PWM1 module

    // Setting of PF2 pin for M1PWM6 channel output pin
    GPIOF->AFSEL |= (1 << 2);           // PF2 sets as alternate function
    GPIOF->PCTL &= ~0x00000F00;         // Set PF2 as output pin
    GPIOF->PCTL |= 0x00000500;          // Make PF2 PWM output pin
    GPIOF->DEN |= (1 << 2);             // Set PF2 as a digital pin

    // ADC Initialization
    SYSCTL->RCGCADC |= 0x01;            // Enable clock to ADC0 module
    GPIOE->AFSEL |= (1 << 3);           // Enable alternate function on PE3
    GPIOE->AMSEL |= (1 << 3);           // Enable analog function on PE3
    ADC0->ACTSS &= ~0x08;               // Disable sample sequencer 3 for configuration
    ADC0->EMUX &= ~0xF000;              // Set SS3 to trigger from processor
    ADC0->SSMUX3 = 0x00;                // Set analog input to Ain0 (PE3)
    ADC0->SSCTL3 = 0x06;                // Set sample control (no TS0, IE0, END0; TS1 to 0, IE1 to 0, END1 to 1)
    ADC0->ACTSS |= 0x08;                // Enable sample sequencer 3

    // PWM Configuration
    PWM1->_3_CTL &= ~(1 << 0);          // Disable Generator 3 counter
    PWM1->_3_CTL &= ~(1 << 1);          // Select down count mode of counter 3
    PWM1->_3_GENA = 0x0000008C;         // Set PWM output when counter reloaded and clear when matches PWMCMPA
    PWM1->_3_LOAD = 5000;               // Set load value for 50Hz (16MHz/64/50Hz = 5000)
    PWM1->_3_CTL = 1;                   // Enable Generator 3 counter
    PWM1->ENABLE = 0x40;                // Enable PWM1 channel 6 output

    while (1) {
        // Start ADC conversion
        ADC0->PSSI = 0x08;

        // Wait for ADC conversion to complete
        while (!(ADC0->RIS & 0x08));

        // Read ADC value
        adc_value = ADC0->SSFIFO3;

        // Clear ADC interrupt
        ADC0->ISC = 0x08;

        // Control duty cycle based on ADC value (adjust range to 60-100%)
        duty_cycle = (adc_value * 4) + 2999;
        if (duty_cycle < 2999) duty_cycle = 2999;
        if (duty_cycle > 4999) duty_cycle = 4999;
        PWM1->_3_CMPA = duty_cycle;

        // Delay for stability
        Delay_ms(10);
    }
}

/* This function generates delay in ms */
/* Calculations are based on a 16MHz system clock frequency */
void Delay_ms(int time_ms) {
    int i, j;
    for (i = 0; i < time_ms; i++)
        for (j = 0; j < 3180; j++)
            {}  // Execute NOP for 1ms
}
