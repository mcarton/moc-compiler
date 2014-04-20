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

def success(message):
    return GREEN + message + NORMAL

def warning(message):
    return YELLOW + message + NORMAL

def error(message):
    return RED + message + NORMAL

def get_cmd(f):
    return 'env SCRIPT_PATH={0} {0}/{1} tests/{2} -w all'.format(MOCC_PATH, MOCC, f)

def run_test(f):
    print('Testing {}... '.format(f), end='')

    with open(os.devnull, 'w') as devnull:
        return_code = subprocess.call(
            get_cmd(f).split(),
            stdout=devnull,
            stderr=devnull
        )

    return return_code

success_files = [os.path.join(dp, f) for dp, dn, filenames in os.walk('success')
                 for f in filenames if os.path.splitext(f)[1] == '.moc']
warning_files = [os.path.join(dp, f) for dp, dn, filenames in os.walk('warning')
                 for f in filenames if os.path.splitext(f)[1] == '.moc']
failure_files =  [os.path.join(dp, f) for dp, dn, filenames in os.walk('failure')
                  for f in filenames if os.path.splitext(f)[1] == '.moc']

nb_fail = 0
nb_success = 0

for f in success_files:
    return_code = run_test(f)

    if return_code == 0:
        nb_success += 1
        print(success('OK'))
    else:
        nb_fail += 1
        print(error('FAIL'))
        print(error('    Expected success, found {}.'.format(
            'warning' if return_code == 1 else 'error')))

for f in warning_files:
    return_code = run_test(f)

    if return_code == 1:
        nb_success += 1
        print(success('OK'))
    else:
        nb_fail += 1
        print(error('FAIL'))
        print(error('    Expected warning, found {}.'.format(
            'success' if return_code == 0 else 'error')))

for f in failure_files:
    return_code = run_test(f)

    if return_code == 2:
        nb_success += 1
        print(success('OK'))
    else:
        nb_fail += 1
        print(error('FAIL'))
        print(error('    Expected error, found {}.'.format(
            'success' if return_code == 0 else 'warning')))

print('\nSummary\n-------')
if nb_fail == 0:
    print(success('Everything is OK.'))
else:
    print(success('{} tests passed successfully.'.format(nb_success)))
    print(error('{} tests failed.'.format(nb_fail)))
    sys.exit(1)
