# Ultrasonic Phased Array (UDB)

Beam-steering firmware for a 39 kHz ultrasonic transducer array, running on an **STM32F756ZG** (Cortex-M7). Built as part of a team project.

## What it does

The array steers its acoustic beam by firing each transducer with a small time offset. A joystick read through the ADC selects the direction: **left, center or right**. The firmware then restarts the transducer drive signals with the matching delays.

## How it works

**Beam-steering math (`phasedArray.cpp`).** For a steering angle θ, adjacent transducers need a phase shift of

```
Δφ = 2π · d · sin(θ) / λ
```

where λ = speed of sound / frequency (343 m/s ÷ 39 kHz ≈ 8.8 mm) and the spacing d defaults to λ/2. The phase shift is converted to a per-element time delay in microseconds.

**Signal generation (`main.cpp`).**
- Eight hardware timers (TIM1–5, TIM9–11) each produce one PWM drive signal.
- A ninth timer (TIM8) is a microsecond counter used for the delays between them.
- Steering left or right means starting the timers in opposite orders, with the calculated delay between each start.
- Changing direction stops and resets all of the timers, then starts them again with the new delays.

**Tests (`unitTestSuite.cpp`).** 14 unit tests cover degree/radian conversion, wavelength, element spacing and the time-delay calculation.

## Files

| File | Purpose |
|---|---|
| `main.cpp` | STM32 HAL setup, timer control, joystick input, main loop |
| `phasedArray.hpp` / `.cpp` | `PhasedArray` class: steering geometry and delay calculation |
| `unitTestSuite.hpp` / `.cpp` | Unit tests for the `PhasedArray` math |
| `phased_array_sdp_v3.ioc` | STM32CubeMX configuration (pins, clocks, timers, ADC) |

## Build

Open `phased_array_sdp_v3.ioc` in **STM32CubeMX** or **STM32CubeIDE**, generate the project, add these sources, then build and flash it to the board.
