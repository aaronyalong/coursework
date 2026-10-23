// ADC Version 1
#include "tm4c123gh6pm.h"


void init(void){

    // Enable clocks
    SYSCTL_RCGCADC_R = 0x1;   // ADC
    SYSCTL_RCGCGPIO_R = 0x18; // GPIO E & D

    // ADC -> GPIO E
    GPIO_PORTE_DEN_R = 0x8; // PE3 enabled (1) -> b01000 -> 0x8
    GPIO_PORTE_AFSEL_R = 0x8; // alternate function PE3~ -> b1000 -> 0x8
    GPIO_PORTE_AMSEL_R = 0x8; // Enable analog function PE3~ -> b1000 -> 0x8

    // ADC 0
    ADC0_ACTSS_R = 0x0; // ASEN3 disable during configuration (ADC Active Sample Sequencer)

    ADC0_EMUX_R = 0x0000;    // Selects trigger source for SS3, 0: processor (default), F: continuously sample
    ADC0_SSMUX3_R = 0x0;     // SS3 analog input configuration, 0: read input
    ADC0_PSSI_R = 0x0;       // Trigger from the processor. Begin sampling on SS3
    ADC0_SSCTL3_R = 0x06;    // Set sample control (no TS0, IE0, END0; TS1 to 0, IE1 to 0, END1 to 1)

    ADC0_ACTSS_R = 0x8; // ASEN3 enabled (ADC Active Sample Sequencer)

    //ADC0_RIS_R // Raw interrupt Status (0: not occured, 1: enabling interrupt)
    //ADC0_ISC_R // Interrupt Status and clear (1: to clear)

    //ADC0_SSFIFO3_R // Result of conversion

    // Port D3 Output
    GPIO_PORTD_DEN_R = 0x8; // PD3 enabled (1) -> b01000 -> 0x8
    GPIO_PORTD_DIR_R = 0x8; // PD3 enabled (1) -> b01000 -> 0x8


}

int main(void)
{
    init();

    unsigned int adc_value;

    while (1){
        ADC0_PSSI_R = 0x8;                 // Start sampling

        while((ADC0_RIS_R & 0x8) == 0) ;   // Wait until sample conversion completed, no interrupt
        adc_value = ADC0_SSFIFO3_R;        // Read ADC conversion
        ADC0_ISC_R = 0x8;                  // Clear interrupt status

        if(adc_value >= 2048)
            GPIO_PORTD_DATA_R  = 0x08;     // LED On
        else if(adc_value < 2048)
            GPIO_PORTD_DATA_R  = 0x0;      // LED Off
    }
}
