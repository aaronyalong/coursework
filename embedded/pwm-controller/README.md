# PWM Controller

Potentiometer-controlled PWM output on the TM4C123GH6PM (Tiva C LaunchPad), written in bare-metal C with direct register access.

## What it does

An analog input on **PE3** is sampled continuously by the ADC. Its value sets the duty cycle of a **50 Hz PWM signal on PF2**, so turning the potentiometer changes the output, e.g. LED brightness or motor/servo speed.

## How it works

| Peripheral | Configuration |
|---|---|
| PWM1, generator 3, channel 6 (M1PWM6) | Down-count mode; system clock ÷ 64 with a load value of 5000, giving 50 Hz |
| ADC0, sample sequencer 3 | Single sample from AIN0 (PE3), triggered by the processor, polled for completion |
| GPIO | PF2 set to its PWM alternate function; PE3 set to analog input |

The main loop starts a conversion, waits for it to finish, maps the 12-bit reading to a compare value (`adc * 4 + 2999`, clamped to 2999–4999, i.e. a 60–100% duty range), writes it to `CMPA`, then waits about 10 ms.

## Files

| File | Purpose |
|---|---|
| `main.c` | Peripheral setup and the control loop |
| `tm4c123gh6pm_startup_ccs.c` | Startup code and vector table |
| `tm4c123gh6pm.cmd` | Linker command file |

## Build

Open it as a project in **Code Composer Studio** targeting the TM4C123GH6PM, then build and flash it to the LaunchPad. Wire a potentiometer to PE3 and an LED or scope to PF2.
