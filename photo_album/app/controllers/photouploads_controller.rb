class PhotouploadsController < ApplicationController
  def index
    @img_paths=[]
    extes=Dir.glob(Rails.root.join('public','*{jpg,jpeg,png}'))

    extes.each do |exte|
      @img_paths.push('/'+File.basename(exte))
    end

    @img_paths.shuffle!
  end


  def create
      uploaded_photo=photoupload_param[:file]
      name=uploaded_photo.original_filename

      check_file=(File.extname(name)=='.jpg'||File.extname(name)=='.jpeg'||File.extname(name)=='.png')

      if check_file
        if (uploaded_photo.size < 1.megabytes)
          output_path=Rails.root.join('public',name)

          File.open(output_path,'w+b')do|fp|
            fp.write uploaded_photo.read
          end
        end
      end

    redirect_to action: 'index'
  end


  private
  def photoupload_param
    params.require(:photoupload).permit(:file)
  end
end
