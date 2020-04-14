import FS, { fstat } from 'fs'
import CP from 'child_process'

import {ShimApiTests} from '@rundeck/testdeck/util/apiShim'

describe('Legacy API Project Tests', ()=> {
    ShimApiTests(/^test-project/)
})