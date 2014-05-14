#!/usr/bin/python3

import os
import subprocess
import sys

# output colors
GREEN = '\033[0;32m'
YELLOW = '\033[0;33m'
RED = '\033[0;31m'
NORMAL = '\033[0;00m'

# MOC compiler
MOCC_PATH = '..'
MOCC = 'mocc'

MACHINES = ['llvm', 'tam']

success_files = [os.path.join(dp, f) for dp, dn, filenames in os.walk('success')
                 for f in filenames if os.path.splitext(f)[1] == '.moc']
warning_files = [os.path.join(dp, f) for dp, dn, filenames in os.walk('warning')
                 for f in filenames if os.path.splitext(f)[1] == '.moc']

failure_files =  [os.path.join(dp, f) for dp, dn, filenames in os.walk('failure')
                  for f in filenames if os.path.splitext(f)[1] == '.moc']

# associate each return code to the correct semantic
RETURN_CODES = {
    0: 'success',
    1: 'warning',
    2: 'error',
}
# and vice versa
RETURN_CODES.update({v:k for k, v in RETURN_CODES.items()})

def success(message):
    return GREEN + message + NORMAL

def warning(message):
    return YELLOW + message + NORMAL

def error(message):
    return RED + message + NORMAL

def get_cmd(f, machine):
    return 'env SCRIPT_PATH={0}  {0}/{1} -w all -m {3} tests/{2}'.format(MOCC_PATH, MOCC, f, machine)

def compile_test(f, machine):
    print('Testing {}... '.format(f), end='')

    with open(os.devnull, 'w') as devnull:
        return_code = subprocess.call(
            get_cmd(f, machine).split(),
            stdout=devnull,
            stderr=devnull
        )

    return return_code

def run_tests(files, machine, expected_compiler_return_code):
    nb_fail = 0
    nb_success = 0

    for f in files:
        compiler_return_code = compile_test(f, machine)

        if compiler_return_code == expected_compiler_return_code:
            nb_success += 1
            print(success('OK'))
        else:
            nb_fail += 1
            print(error('FAIL'))
            print(error('    Expected success, found {}.'.format(RETURN_CODES[compiler_return_code])))

    return nb_fail, nb_success

def run_all_tests(machine):
    s = '{} machine'.format(machine.upper())
    print(s + '\n' + '-' * len(s))

    nb_fail = 0
    nb_success = 0

    a, b = run_tests(success_files, machine, RETURN_CODES['success'])
    nb_fail, nb_success = nb_fail + a, nb_success + b
    a, b = run_tests(warning_files, machine, RETURN_CODES['warning'])
    nb_fail, nb_success = nb_fail + a, nb_success + b
    a, b = run_tests(failure_files, machine, RETURN_CODES['error'])
    nb_fail, nb_success = nb_fail + a, nb_success + b

    return nb_fail, nb_success

if __name__ == '__main__':
    nb_fail = 0
    nb_success = 0

    try:
        machine = sys.argv[1]
        if machine not in MACHINES:
            raise Exception('This machine is not supported yet. If you want it to be supported, please code it and make a pull request :)')
        nb_fail, nb_success = run_all_tests(machine)
    except IndexError:
        for machine in MACHINES:
            tmp0, tmp1 = run_all_tests(machine)
            nb_fail += tmp0
            nb_success += tmp1
            print()
    except Exception as e:
        print(e)
        exit(1)

    print('\nSummary\n-------')
    if nb_fail == 0:
        print(success('Everything is OK.'))
    else:
        print(success('{} tests passed successfully.'.format(nb_success)))
        print(error('{} tests failed.'.format(nb_fail)))
