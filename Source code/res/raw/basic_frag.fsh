varying lowp vec2 texCoordinate;
uniform sampler2D texture;

void main()
{
	gl_FragColor = texture2D(texture, texCoordinate);
}
