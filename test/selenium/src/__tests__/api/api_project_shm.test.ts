import FS, { fstat } from 'fs'
import CP from 'child_process'

import {ShimApiTests} from 'util/apiShim'

import {envOpts} from 'test/selenium'

describe('Legacy API Project Tests', ()=> {
    ShimApiTests(/^test-project/)
})