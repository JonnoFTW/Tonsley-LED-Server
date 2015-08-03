function Tonsley_Game_of_Life3( Ninit)

% game of life for the ledwall at Tonsley
% algorithm for game of life adapted from Matlab's standard distribution

% control flow definitions
verbose = true;
led_wall = false;
figure_window = true;
data_receive = true;
exit_modifiers = { 'shift', 'control', 'alt'};

if data_receive
    javaaddpath '/scratch/asworkspace/TonsleyThread/bin/';
    port = 7778;
    dataAcceptThread = javaObjectEDT('DataAcceptThread',port,verbose);
    dataAcceptThread.start()
end
%#ok<*UNRCH>

% parse input arguments
if ~exist( 'Ninit', 'var') || isempty( Ninit)
    Ninit = 17;
end

% led definitions
N = [ 165 17];
% N = [ 201 51];
pause_time = 1;
colour = { 'red', 'green', 'blue'};

% derived
scrsz = get( 0, 'ScreenSize');

% friendly start up
if verbose,
    fprintf( 'Starting led_demo\n');
    if led_wall
        fprintf( '  Driving the led wall via tcpip\n');
    else
        fprintf( '  Not driving the led wall\n');
    end
    if figure_window
        fprintf( '  Simulating in a figure window\n');
    else
        fprintf( '  Not simulating in a figure window\n');
    end
end

% open communications to the led wall
if led_wall
    if verbose, fprintf( 'Opening communications to the led wall\n'); end
    htcpip = Tonsley_leds( 'open');
end

% set up initial matrix for the algorithm and image for display
X = zeros( N( 2), N( 1), 3);
p = -1:1;

for count = 1:Ninit,
    kx = floor( rand * ( N( 2) - 4)) + 2;
    ky = floor( rand * ( N( 1) - 4)) + 2;
    for i = 1:3,
        X( kx + p, ky + p, i) = ( rand( 3) > 0.5);
    end
end
% Here we generate index vectors for four of the eight neighbors.
% We use periodic (torus) boundary conditions at the edges of the universe.
n = [ N( 2) 1:N( 2) - 1];
e = [ 2:N( 1) 1];
s = [ 2:N( 2) 1];
w = [ N( 1) 1:N( 1) - 1];

% prepare figure window
if figure_window
    
    % work out where to put the image
    figpos = [ scrsz( 1) + 10, 100, scrsz( 3) - 10, scrsz( 4) - 100];
    if figpos( 4) / figpos( 3) > N( 1) / N( 2),
        figpos( 4) = ceil( figpos( 3) * N( 2) / N( 1));
    else
        figpos( 3) = ceil( figpos( 4) * N( 1) / N( 2));
    end
    figpos( 3:4) = figpos( 3:4) / 2;
   % closeF = @(dat)dat.stopThread();
    % set up figure window
    hf = figure( 1);
   % set( hf, 'DeleteFcn', closeF(dataAcceptThread));
    set( hf, 'Position', figpos);
    plothandle = zeros( 1, 3);
    for c = 1:3,
        [ j, i] = find( X( :, :, c));
        plothandle( c) = plot( i, j, '.', 'Color', colour{ c}, 'MarkerSize', 60);
        hold on
    end
    axis( [ 0 N( 1) + 1 0 N( 2) + 1]);
%     hI = image( I);
    set( gca, 'Position', [ 0 0 1 1], 'Visible', 'off');
end

% loop
if verbose, fprintf( 'Commencing loop\n'); end
while ~isempty( setxor( get( hf, 'CurrentModifier'), exit_modifiers))
    % read in mobile data
    if data_receive
        things = dataAcceptThread.getThings();
        for idx = 1:numel(things);
            thing = things(idx);
            [rows,cols] = size(thing.board);
            x = thing.y+1;
            y = thing.x+1;
            z = thing.z+1;
            fprintf('Adding board x=%d y=%d z=%d\n',x,y,z);
            %disp(thing.board);
            for i=1:rows
                for j=1:cols
                  X(x+i,y+j,z)= thing.board(i,j);
                end
            end
        end
    end
    % loop over colours
    for c = 1:3,
        
        % How many of eight neighbors are alive?
        N = X( n, :, c) + X( s, :, c) + X( :, e, c) + X( :, w, c) + ...
            X( n, e, c) + X( n, w, c) + X( s, e, c) + X( s, w, c);
        
        % A live cell with two live neighbors, or any cell with three
        % neigbhors, is alive at the next time step.
        X( :, :, c) = ( X( :, :, c) & ( N == 2)) | ( N == 3);
        
        % update figure window and force redraw
        if figure_window
            [ j, i] = find( X( :, :, c));
            set( plothandle( c), 'Xdata', i, 'Ydata', j)
            drawnow
        end
        
    end
   
    % turn the image into a packet of data to send to the led wall
    if led_wall
        if verbose, fprintf( 'Writing to leds\n'); end
        image2leds( X, htcpip);
    end
    
    % and wait
    pause( pause_time);  
end
% close the java data accept thread
if data_receive
    dataAcceptThread.stopThread();
end
% close communications to the led wall
if led_wall
    image2leds( zeros( size( I)), htcpip);
    if verbose, fprintf( 'Closing communications with led wall\n'); end
    Tonsley_leds( 'close', htcpip);
end

% close figure windows
if figure_window
    close( hf);
end

end
