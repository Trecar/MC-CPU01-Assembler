# MC-CPU01-Assembler
Simple Assembler for my Minecraft Redstone CPU. Assembles directly to .schem
More about the CPU can be read at https://forum.openredstone.org/thread-14573.html

## Workflow
1. Write your program and save as .txt **in same Folder as CPU01-ASB.jar**
2. Upon executing the .jar, it will create a .schem of the same filename for every .txt it can assemble
3. Upload/Add/Whatever the .schem to your Worldedit Schematics directory
4. **//paste -a** while standing on the **white terracotta block** at the top of the ROM. (Should be easy to find)

## Syntax etc.
Generally: The Assembler is case insensitive, but order matters unless clearly stated otherwise.
Every new Instruction has to be on a new line, there shall be no empty lines. Words are separated by spaces. You can use as many spaces in each line as you please, the Assembler does not care.

### Number Representation
{x Bit} means you are meant to specify a number. You can specify it either in decimal, hex or binary.
**Every Number needs the corresponding Suffix d, h or b!!!**
Numbers do not need to match the required bitwidth. 0's will be added accordingly.
8-Bit decimals can also have a negative sign (-> 2's Complement) ==> Range is either 0d to 255d or -128d to 127d
Examples: F9h, 1001b, -24d, 80d

### Options
<Options> sit at the end of Instructions. Their order among each other does not matter **but they must be at the end!**
If you want them to take effect, write them, otherwise leave them out.
**When coding, write them without <>. This is only to highlight them!**

### Instructions
X, Y, Z: Registers. (0-7)
ß: IO Port (0-3)

Syntax | Description
-------------------------------------------------------|-----------
`NOP` | Does nothing. Essentially a "Wait" Instruction
`ADD lrX lrY srZ` <add1> | Register Z = RegX + RegY. Optionally also adds 1 to Z
`ADC lrX lrY srZ` | Adds with Carry (Z=X+Y)
`SUB lrX lrY srZ` | Z=X-Y
`(logic) lrX lrY srZ` | Z= X (logic) Y ,logic: `OR`, `NAND`, `XOR`, `AND`
`SHR lrX srZ` | logical right shift of X, stored to Z
`LIM srX {8-Bit}` | Load Immediate
`ADDI lsX {8-Bit}` | X=X+Number Stores to same location as it loaded
`SUBI lsX {8-Bit}` | X=X-Number
`(logic w/ Imm) lsX {8-Bit}` | X= X (logic) number ,logic: `ORI`, `NANDI`, `XORI`, `XORI`
`SWP lrX {**5**-Bit} srY` | Memory Swap: Contents of X to Cell {Number}, Cell's contents to Y
`SWPNT lrX srY` | Swap using Pointer as address. Pointer value is the content the previous instruction loaded as its first Input. You can also use this with `SWP` by attaching `<pnt>`, this will OR the Number with the pointer.
`JMP {**6**-Bit} <st>` | Jump to specified Instruction. **The CPU has a 2-Instruction Branch delay.** *st* will stall these next 2 instructions **if** the branch is taken. (Always true for JMP). Otherwise they will execute.
`BEQ {6-Bit} <st> <ns>` | Upon A-B, branch if A equals B. *ns* will disable the previous instructions store. Very useful when comparing with Immediates or just saving register space.
`BGT {6-Bit} <st> <ns>` | branch if A>B
`BGE {6-Bit} <st> <ns>` | branch if A>=B (Carry Out Flag)
`BUE {6-Bit} <st> <ns>` | branch if A!=B (Zero flag=false)
`BZR {6-Bit} <st> <ns>` | branch if prev. Result all 0d (Zero flag=true)
`BTR {6-Bit} <st> <ns>` | branch if prev. Result all 1s /255d /FFh (255 flag=true)
`BNT {6-Bit} <st> <ns>` | branch if prev. Result not 255d (255 flag=false)
`BUF {6-Bit} <st> <ns>` | branch if underflow (After `SHR`)
`INP pß srX {6-Bit Number <st>}` | If new Input at input port ß, store in Reg. X. Otherwise branch to specified Instruction
`OUT lrX pß <OiR>` | Contents of Reg. X to output port ß. *OiR*: Only do this **if** the device requests it. No Output being executed will be indicated by setting the Zero-Flag to false.
`SETUP <zrr> <2C>` | *zrr*: Set Register 0 to Zero Register. Reading from r0 will return 0, but it will still store. Leaving *zrr* out will set it to a GPR. *2C*: Set decimal display mapped to Memory Cell 15d / Fh to 2's Complement Mode (Range: -128 to 127). If left out, will set display to 0's Complement (Range: 0 to 255)
  
### Writing Code
Whatever your text editor might say, **instruction adressing start at 0**. Your code can have a **maxium of 64 instructions** (0d to 63d). 

## Source Code
To create Schematics, Querz' wonderful NBT library is used. (github.com/querz/NBT)
  
  
