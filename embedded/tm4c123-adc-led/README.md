# TM4C123 ADC LED Control

Analog threshold detection on the TM4C123GH6PM (Tiva C LaunchPad), in bare-metal C with direct register access.

## What it does

The ADC continuously samples an analog signal on **PE3**. If the reading is at or above half scale (2048 of 4095, about 1.65 V), an LED on **PD3** turns on; below that, it turns off.

## How it works

- **Clocks:** enables ADC0 and GPIO ports D and E.
- **ADC0, sample sequencer 3:** a single sample from AIN0 (PE3), triggered by the processor. Completion is detected by polling the raw interrupt status rather than using interrupts.
- **Loop:** start a conversion, wait for `RIS`, read `SSFIFO3`, clear the status, and set or clear PD3 by comparing against the threshold.

## Files

| File | Purpose |
|---|---|
| `main.c` | Initialization and the polling loop |
| `tm4c123gh6pm_startup_ccs.c` | Startup code and vector table |
| `tm4c123gh6pm.cmd` | Linker command file |

## Build

Open it as a project in **Code Composer Studio** targeting the TM4C123GH6PM, then build and flash it to the LaunchPad. Connect a potentiometer or other analog source to PE3 and an LED, through a resistor, to PD3.
