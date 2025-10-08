clear;
output_precision(16);

function retval = accoeff (n)
  retval = factorial(2*n) / ((4^n)*(factorial(n)^2)*(2*n+1));
endfunction

for m = 1:72
  x = num2hex(accoeff(m))
endfor


