The MavericksMockPrinter.kts script is a utility for generating mock state files for Mavericks ViewModels.

## Usage
For convenience, a prepackaged executable is provided that can be used to run the script.
Just execute `./MavericksMockPrinter` from the command line.

The script can also be run with Kscript - `kscript MavericksMockPrinter.kts`
Kscript was used to generate the executable - `kscript --package MavericksMockPrinter.kts`

## Arguments
Pass the -h flag to get help and see all available arguments to the script
`./MavericksMockPrinter -h`

## Setup
You must have a device connected via adb with debugging enabled.
Open your app to a Mavericks screen and execute the script. State files for all running Mavericks views
will be generated.
Follow the script help and output for more details.

## Building

The executable can be rebuilt by running the `package` script
in this directory (you must have kotlin and kscript installed).