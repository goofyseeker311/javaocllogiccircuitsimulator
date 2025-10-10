clear;
output_precision(16);

function retval =  sinenewt ( x, y )
  retval = x - (sin(x) - y) / cos(x);
endfunction

function retval =  tannewt ( x, y )
  retval = x - (tan(x) - y) * (cos(x))^2;
endfunction


