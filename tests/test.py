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

def get_cmd(f):
    return 'env SCRIPT_PATH={0}  {0}/{1} -w all -m {3} tests/{2}'.format(MOCC_PATH, MOCC, f, sys.argv[1])

def run_test(f):
    print('Testing {}... '.format(f), end='')

    with open(os.devnull, 'w') as devnull:
        return_code = subprocess.call(
            get_cmd(f).split(),
            stdout=devnull,
            stderr=devnull
        )

    return return_code

def run_tests(files, expected_return_code):
    nb_fail = 0
    nb_success = 0

    for f in files:
        return_code = run_test(f)

        if return_code == expected_return_code:
            nb_success += 1
            print(success('OK'))
        else:
            nb_fail += 1
            print(error('FAIL'))
            print(error('    Expected success, found {}.'.format(RETURN_CODES[return_code])))

    return nb_fail, nb_success

if __name__ == '__main__':
    nb_fail = 0
    nb_success = 0

    a, b = run_tests(success_files, RETURN_CODES['success'])
    nb_fail, nb_success = nb_fail + a, nb_success + b
    a, b = run_tests(warning_files, RETURN_CODES['warning'])
    nb_fail, nb_success = nb_fail + a, nb_success + b
    a, b = run_tests(failure_files, RETURN_CODES['error'])
    nb_fail, nb_success = nb_fail + a, nb_success + b

    print('\nSummary\n-------')
    if nb_fail == 0:
        print(success('Everything is OK.'))
    else:
        print(success('{} tests passed successfully.'.format(nb_success)))
        print(error('{} tests failed.'.format(nb_fail)))
        sys.exit(1)
