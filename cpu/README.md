# Simple CPU

Implementation of a very simple cpu in scala. The cpu design was slightly inspired by picomips. This work is licenced under the GPLv3 or any later version of the GPL as published by the Free Software Foundaiton (https://fsf.org). 

A copy of the GPLv3 is provided in the LICENCE file.

# Building
If you don't have scala and sbt working then check their project's websites or your distro's repos for packages.

Once you have scala and sbt working, you can build and test the cpu by just running make

# Configuration
Configuration will be done by setting the parameters the CPU is instanced with. The wordsize should be double the address length of a general purpose register (for the ldi instruction).

# Instructions
The instruction length is configurable: the number of bits for the opcode is the logarithm to the base 2 of opcodes.numOps (rounded up). The operand is twice the length of the address of a general purpose register. All instructions are fixed length. If the second argument is not needed then some bits must still be there as padding. 

The opcodes are described below. See Decoder.scala for the opcodes object.

## nop
Does nothing.

## ld
This saves whatever the output of the ALU is to the general purpose register addressed in the argument. In this way the output of the ALU should be thought of as an accumulator. 

## ldi
Instructs the ALU to load the immediate value (given as a single argument taking up all of the space for the one argument) to it's output. The value should be saved on the next clock cycle using ld (see the example).

## add, sub, or, xor
Perform the named operation on two integers, the two arguments are addresses of registers to be added together. The result is placed on the output of the ALU (which should be thought of as the accumulator). This result can then be saved to a register on the next clock cycle using ld.

## Addressed Addition Example 
```
// load 
ldi 4 // load immediate value 4 into address 1
ld 1

ldi 5 // load immediate value 5 into address 2
ld 2

add 1 2 // do the addition
ld 1 // save the result in address 1. This result will be 5+4=9
```

## addi, subi, ori, xori
Perform the named operatoin on two integers. One is addressed and another is loaded directly. The address to be loaded is given as the first arguement and the immediate value (note that this must be no larger than a general purpose regsiter address) is given as the second arguement. The result goes to the output of the ALU and should be saved on the next clock cycle using ld. These instructions allow clock cycles to be saved when only operating on small numbers.

## imediate decrement example
```
ldi 6 // load the number to be decremented into register 1
ld 1

subi 1 1
ld 1 // save into register 1
```

## not
Calculates the logical not of the addressed memory location and puts it on the ALU's output and puts it on the ALU's output

## jr
Jumps to a value relative to the current program counter value. The number to be added to the program counter is given as the double length arguement.
TODO: make the arguement signed?? Load arguement from register?

## ji
Jumps to the address given as a single double length arguement
