import yargs from 'yargs'

yargs.commandDir('../../testdeck/src/commands', {extensions: ['ts']}).demandCommand().help().argv