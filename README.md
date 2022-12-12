# Chisel Torch
An Chisel implementation of a PyTorch-Like Interface for Accelerator Generation

## Installation
0. Follow instructions (https://sdkman.io) on how to install SDKMan. `sdk` should be in your path after installation.

<br>

1. Install JDK and SBT
    ```bash
    sdk install java 22.3.r11-grl
    sdk install sbt
    ```
    To verify JDK and sbt is properly installed:
    ```bash
    java -version
    sdk version
    ```

<br>

2. Install Berkeley Hardfloat from Apex's Berkeley Hardfloat Branch
    ```bash
    git clone git@github.com:Entropy-xcy/berkeley-hardfloat.git
    cd berkeley-hardfloat
    sbt publishLocal
    ```
    Note: during installing berkely-hardfow, a non-critical warning `-Xsource is outdated` might appear. Change `-Xsource: 2.11` to `-Xsource: 2.13` in `build.sbt` can get rid of the warning.

<br>

3. Clone and Run ChiselTorch
    ```bash
    git clone git@github.com:Entropy-xcy/chisel-torch.git
    cd chisel-torch
    sbt "runMain chiseltorch.nn.module.SequentialBuild"
    ```
After that, `Sequential.fir` and `Sequential.v`should be available in the current working directory. 

