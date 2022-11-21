# Chisel Torch
An Chisel implementation of a PyTorch-Like Interface for Accelerator Generation

## Installation
0. Follow instructions (https://sdkman.io) on how to install SDKMan. `sdk` should be in your path after installation.

1. Install JDK and SBT
```bash
sdk install java 22.3.r11-grl
sdk install sbt
```
To verify JDK and sbt is properly installed:
```
java -version
sdk --version
```

2. Install Berkeley Hardfloat from Apex's Berkeley Hardfloat Branch
```bash
git clone git@github.com:Entropy-xcy/berkeley-hardfloat.git
cd berkeley-hardfloat
sbt assembly
```

3. Clone and Run ChiselTorch
```bash
git clone git@github.com:Entropy-xcy/chisel-torch.git
cd chisel-torch
sbt "runMain chiseltorch.nn.module.SequentialBuild"
```
After that, `Sequential.fir` should be available in the current working directory. 

